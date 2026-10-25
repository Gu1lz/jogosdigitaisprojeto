
import os
import unicodedata
from pathlib import Path

import requests
import yaml

TOKEN = os.environ["GH_TOKEN"]
OWNER = os.getenv("PROJECT_OWNER", "Gu1lz")
PROJECT_NUMBER = int(os.getenv("PROJECT_NUMBER", "5"))
ENDPOINT = "https://api.github.com/graphql"
YAML_PATH = Path(".github/kanban-status.yml")

session = requests.Session()
session.headers.update({
    "Authorization": f"Bearer {TOKEN}",
    "Content-Type": "application/json",
})


def graphql(query, variables=None):
    response = session.post(
        ENDPOINT,
        json={"query": query, "variables": variables or {}},
        timeout=60,
    )
    response.raise_for_status()
    result = response.json()

    if result.get("errors"):
        raise RuntimeError(
            str(result["errors"])
        )

    return result["data"]


def normalize(value):
    value = unicodedata.normalize("NFKD", str(value or ""))
    return "".join(c for c in value if not unicodedata.combining(c)).lower().strip()


PROJECT_QUERY = """
query($owner: String!, $number: Int!, $cursor: String) {
  user(login: $owner) {
    projectV2(number: $number) {
      id
      title
      fields(first: 100) {
        nodes {
          __typename
          ... on ProjectV2Field {
            id
            name
          }
          ... on ProjectV2SingleSelectField {
            id
            name
            options {
              id
              name
            }
          }
        }
      }
      items(first: 100, after: $cursor) {
        nodes {
          id
        }
        pageInfo {
          hasNextPage
          endCursor
        }
      }
    }
  }
}
"""


def load_project():
    variables = {
        "owner": OWNER,
        "number": PROJECT_NUMBER,
        "cursor": None,
    }
    data = graphql(PROJECT_QUERY, variables)
    project = data["user"]["projectV2"]

    if project is None:
        raise RuntimeError("Projeto GitHub não encontrado.")

    items = []
    page = project["items"]

    while True:
        items.extend(page["nodes"])

        if not page["pageInfo"]["hasNextPage"]:
            break

        variables["cursor"] = page["pageInfo"]["endCursor"]
        page = graphql(PROJECT_QUERY, variables)["user"]["projectV2"]["items"]

    project["all_items"] = items
    return project


def set_field_value(project_id, item_id, field_id, value):
    graphql("""
    mutation(
      $project: ID!,
      $item: ID!,
      $field: ID!,
      $value: ProjectV2FieldValue!
    ) {
      updateProjectV2ItemFieldValue(input: {
        projectId: $project,
        itemId: $item,
        fieldId: $field,
        value: $value
      }) {
        projectV2Item {
          id
        }
      }
    }
    """, {
        "project": project_id,
        "item": item_id,
        "field": field_id,
        "value": value,
    })


def main():
    if not YAML_PATH.exists():
        raise RuntimeError(f"Arquivo não encontrado: {YAML_PATH}")

    with YAML_PATH.open(encoding="utf-8") as file:
        config = yaml.safe_load(file) or {}

    tasks = config.get("tasks", [])

    if not tasks:
        raise RuntimeError(
            "Nenhuma tarefa no YAML. Recriação cancelada."
        )

    project = load_project()
    project_id = project["id"]
    fields = {
        normalize(field["name"]): field
        for field in project["fields"]["nodes"]
    }

    # Proteção contra exclusão acidental por erro de configuração.
    confirm = os.getenv("CONFIRM_RECREATE", "").lower()
    if confirm != "true":
        raise RuntimeError(
            "Defina CONFIRM_RECREATE=true para autorizar a exclusão "
            "dos itens existentes e a recriação do quadro."
        )

    # Excluir todos os itens existentes, inclusive itens de issues/PRs.
    for item in project["all_items"]:
        graphql("""
        mutation($project: ID!, $item: ID!) {
          deleteProjectV2Item(input: {
            projectId: $project,
            itemId: $item
          }) {
            deletedItemId
          }
        }
        """, {
            "project": project_id,
            "item": item["id"],
        })
        print(f"Item removido: {item['id']}")

    status_definitions = {
        str(status["id"]): str(status["name"])
        for status in config.get("statuses", [])
    }

    aliases = {
        "fila": ["backlog"],
        "a_fazer": ["ready", "todo", "to do", "a fazer"],
        "em_andamento": ["in progress", "doing", "em andamento"],
        "em_revisao": ["in review", "review", "em revisao"],
        "concluido": ["done", "complete", "completed", "concluido"],
    }

    def set_status(item_id, status_id):
        field = fields.get("status")
        if not field:
            print("Aviso: campo Status não encontrado.")
            return

        desired = normalize(status_definitions.get(status_id, status_id))
        candidates = {desired}
        candidates.update(
            normalize(value) for value in aliases.get(status_id, [])
        )

        option = next(
            (
                option for option in field.get("options", [])
                if normalize(option["name"]) in candidates
            ),
            None,
        )

        if option is None:
            raise RuntimeError(
                f"Status '{status_id}' não corresponde a nenhuma opção "
                "do campo Status. Nenhum cartão será criado além dos "
                "já processados."
            )

        set_field_value(
            project_id,
            item_id,
            field["id"],
            {"singleSelectOptionId": option["id"]},
        )

    def set_date(item_id, possible_names, date_value):
        if not date_value:
            return

        field = next(
            (
                fields[name]
                for name in possible_names
                if name in fields
            ),
            None,
        )

        if field is None:
            print(
                "Aviso: campo de data não encontrado: "
                + ", ".join(possible_names)
            )
            return

        set_field_value(
            project_id,
            item_id,
            field["id"],
            {"date": str(date_value)},
        )

    for task in tasks:
        title = str(task["title"]).strip()
        if not title:
            raise RuntimeError("Foi encontrada uma tarefa sem título.")

        start = task.get("start_date") or task.get("date") or ""
        due = task.get("due_date") or task.get("date") or ""
        status = str(task.get("status", "a_fazer"))
        notes = str(task.get("notes", ""))

        body = (
            f"Início: {start or 'Não definido'}\n"
            f"Prazo: {due or 'Não definido'}\n"
            f"Status planejado: {status}\n\n"
            f"{notes}"
        )

        result = graphql("""
        mutation($project: ID!, $title: String!, $body: String!) {
          addProjectV2DraftIssue(input: {
            projectId: $project,
            title: $title,
            body: $body
          }) {
            projectItem {
              id
            }
          }
        }
        """, {
            "project": project_id,
            "title": title,
            "body": body,
        })

        item_id = result["addProjectV2DraftIssue"]["projectItem"]["id"]

        set_status(item_id, status)
        set_date(item_id, ["start date"], start)
        set_date(item_id, ["target date", "due date"], due)

        print(f"Cartão criado: {title}")

    print(
        f"Recriação concluída. Total de tarefas processadas: {len(tasks)}"
    )


if __name__ == "__main__":
    main()
