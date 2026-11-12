"""Verifica o jogo principal no Linux usando uma porta serial virtual e renderizacao real."""
import os
from pathlib import Path
import pty
import subprocess
import tempfile

ROOT = Path(__file__).resolve().parents[1]
PROJECT = ROOT / 'laboratorio/lab7/geope'
MESSAGES = {
    'partial': b'TILT:',
    'left': b'-1\r\n',
    'center': b'TILT:0\n',
    'right': b'TILT:1\n',
    'noise': b'\xff\xfe boot\nINFO: AX=5000\nTILT:2\n',
    'error': b'ERRO: MPU6050 etapa=LER_ACCEL endereco=0x68\n',
    'recover': b'TILT:0\n',
    'right_paused': b'TILT:1\n',
    'left_dead': b'TILT:-1\n',
    'dead_center': b'TILT:0\n',
    'left_restart': b'TILT:-1\n',
    'after_reconnect': b'TILT:1\n',
    'ready': b'READY\n',
    'final_center': b'TILT:0\n',
}


def main():
    with tempfile.TemporaryDirectory(prefix='headrush-integracao-') as work:
        alias = Path(work) / 'esp32-virtual'
        output = os.environ.get('HEADRUSH_CHECK_OUTPUT', work)
        Path(output).mkdir(parents=True, exist_ok=True)
        master, slave = pty.openpty()
        alias.symlink_to(os.ttyname(slave))
        process = subprocess.Popen([
            'bash', str(PROJECT / 'gradlew'), 'lwjgl3:verificarIntegracaoArduino',
            '-Darduino.port=' + str(alias), '-Darduino.check.output=' + output,
            '--console=plain',
        ], cwd=PROJECT, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True)
        passed = False
        try:
            for line in process.stdout:
                print(line, end='', flush=True)
                if line.strip() == 'PASS ALL':
                    passed = True
                if not line.startswith('REQUEST '):
                    continue
                request = line.strip().split(' ', 1)[1]
                if request == 'disconnect':
                    os.close(master)
                    master = None
                elif request == 'reconnect':
                    os.close(slave)
                    master, slave = pty.openpty()
                    alias.unlink()
                    alias.symlink_to(os.ttyname(slave))
                else:
                    os.write(master, MESSAGES[request])
            result = process.wait()
            if result or not passed:
                raise SystemExit(result or 1)
            print('Integracao do jogo principal verificada com porta serial virtual.')
        finally:
            if process.poll() is None:
                process.terminate()
                process.wait(timeout=10)
            os.close(slave)
            if master is not None:
                os.close(master)


if __name__ == '__main__':
    main()
