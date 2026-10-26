#!/usr/bin/env python3
"""Fail-closed Git index/history scanner. Never print secret contents."""
import json
import os
from pathlib import Path, PurePosixPath
import re
import shutil
import subprocess
import sys
import tempfile


def git(*args):
    return subprocess.check_output(['git', *args], stderr=subprocess.PIPE)


def blocked(name):
    p = PurePosixPath(name)
    n = p.name.lower()
    if any(x.lower() in {'.idea', '.gradle', '.run', 'build', 'out', 'secrets',
                         'credentials', 'dumps', 'backups', 'logs', 'log'} for x in p.parts):
        return True
    # Runtime captures may contain personal data that secret patterns cannot detect.
    if (re.search(r'\.log(?:$|[._-])', n) or re.search(r'\.out(?:$|\.)', n)
            or n.endswith(('.hprof', '.jfr', '.har', '.pcap', '.pcapng'))
            or n.startswith(('hs_err_pid', 'replay_pid'))):
        return True
    if re.fullmatch(r'(application|bootstrap).*\.(yaml|yml|properties)', n):
        return n not in {'application-example.yaml', 'application-example.yml',
                         'application-example.properties'}
    if n == '.env' or n.startswith('.env.'):
        return n != '.env.example'
    return (n in {'id_rsa', 'id_ed25519', 'http-client.private.env.json', '.ds_store', 'db-local.properties', 'persistence.xml'}
            or n.startswith('credentials') or 'service-account' in n or 'serviceaccount' in n
            or n.endswith(('.pem', '.key', '.p12', '.pfx', '.jks', '.keystore',
                           '.dump', '.sql.gz', '.sql.zip', '.db', '.sqlite', '.sqlite3',
                           '.log', '.bak', '.class', '.iml', '.local.properties')))


def check_paths(names):
    failures = [n for n in names if blocked(n)]
    for n in failures:
        print('BLOCKED local/private file: ' + n, file=sys.stderr)
    if failures:
        raise RuntimeError('Remove these files from Git tracking; keep local copies.')


def scan(command, temp):
    binary = Path(git('rev-parse', '--git-path', 'tools/gitleaks').decode().strip()).resolve()
    executable = str(binary) if binary.is_file() else shutil.which('gitleaks')
    if not executable:
        raise RuntimeError('Gitleaks is missing. Install it; commit/push is blocked.')
    report = Path(temp) / 'report.json'
    empty_ignore = Path(temp) / 'empty-ignore'
    empty_ignore.write_text('')
    result = subprocess.run([executable, *command, '--config', str(ROOT / '.gitleaks.toml'),
                             '--redact=100', '--no-banner', '--no-color',
                             '--ignore-gitleaks-allow', '--gitleaks-ignore-path', str(empty_ignore),
                             '--max-archive-depth', '2', '--max-decode-depth', '5',
                             '--report-format', 'json', '--report-path', str(report)],
                            stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    # Do not relay scanner logs; emit only selected metadata.
    findings = json.loads(report.read_text()) if report.exists() else None
    if findings:
        for item in findings:
            filename = item.get('File', '?')
            filename = filename.removeprefix(str(Path(temp) / 'index') + '/')
            print('BLOCKED {}:{} [{}]'.format(filename, item.get('StartLine', '?'),
                                              item.get('RuleID', '?')), file=sys.stderr)
        raise RuntimeError('Possible secret found. Move it to ignored local configuration.')
    if result.returncode != 0 or findings is None:
        raise RuntimeError('Secret scanner failed. Commit/push blocked; check tool/configuration.')


def index_scan():
    entries = []
    for entry in git('ls-files', '--stage', '-z').split(b'\0'):
        if not entry:
            continue
        meta, name = entry.split(b'\t', 1)
        mode, oid, stage = meta.decode().split()
        if stage != '0':
            raise RuntimeError('Resolve merge conflicts before committing.')
        if mode not in {'100644', '100755'}:
            raise RuntimeError('Symlinks/submodules require separate security review.')
        entries.append((oid, name.decode()))
    check_paths([n for _, n in entries])
    with tempfile.TemporaryDirectory(prefix='kdt-secret-') as temp:
        snapshot = Path(temp) / 'index'
        snapshot.mkdir()
        for oid, name in entries:
            target = snapshot / name
            target.parent.mkdir(parents=True, exist_ok=True)
            target.write_bytes(git('cat-file', 'blob', oid))
        scan(['dir', str(snapshot)], temp)


def push_scan():
    heads = []
    for line in sys.stdin:
        fields = line.split()
        if len(fields) != 4 or not re.fullmatch(r'[a-f0-9]{40,64}', fields[1]):
            raise RuntimeError('Invalid pre-push input.')
        if set(fields[1]) != {'0'}:
            heads.append(fields[1])
    if not heads:
        return
    # Scan every reachable commit, including secrets deleted in later commits.
    commits = git('rev-list', *heads).decode().splitlines()
    for commit in commits:
        names = git('ls-tree', '-r', '--name-only', '-z', commit).decode().split('\0')
        check_paths([n for n in names if n])
    with tempfile.TemporaryDirectory(prefix='kdt-secret-') as temp:
        scan(['git', str(ROOT), '--log-opts=' + ' '.join(heads)], temp)


if __name__ == '__main__':
    try:
        ROOT = Path(git('rev-parse', '--show-toplevel').decode().strip())
        os.chdir(ROOT)
        if len(sys.argv) != 2 or sys.argv[1] not in {'index', 'push'}:
            raise RuntimeError('Usage: secret_guard.py index|push')
        index_scan() if sys.argv[1] == 'index' else push_scan()
        print('Secret check passed.')
    except Exception as error:
        # Exception text from Git/scanner might contain secrets: do not echo it.
        if isinstance(error, RuntimeError):
            print(str(error), file=sys.stderr)
        else:
            print('Secret check could not complete; commit/push blocked.', file=sys.stderr)
        sys.exit(1)
