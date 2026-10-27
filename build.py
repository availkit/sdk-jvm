#!/usr/bin/env python3
"""Serial deterministic hosted Maven/artifact packaging; never publish a registry or read credentials."""

import argparse
import hashlib
import json
import pathlib
import shutil
import stat
import subprocess
import zipfile

VERSION = "0.1.1"
STAMP = (2026, 10, 25, 0, 0, 0)

def secure_file(root, relative):
    """Reject a selected symlink at any level before reading package bytes."""
    path = root / relative
    for component in (path, *path.parents):
        if component.is_symlink():
            raise ValueError("Symlink is not a package input")
        if component == root:
            break
    if not stat.S_ISREG(path.stat().st_mode):
        raise ValueError("Package input must be a regular file")
    return path

def write_examples(root, archive):
    inputs = [(relative, secure_file(root, relative)) for relative in EXAMPLE_FILES]
    with zipfile.ZipFile(archive, "w", zipfile.ZIP_DEFLATED, compresslevel=9) as output:
        for relative, source in inputs:
            entry = zipfile.ZipInfo(relative, STAMP)
            entry.compress_type = zipfile.ZIP_DEFLATED
            entry.external_attr = 0o100644 << 16
            output.writestr(entry, source.read_bytes())

def digest(path, algorithm="sha256"):
    return hashlib.new(algorithm, path.read_bytes()).hexdigest()

EXAMPLE_FILES = ('coverage.json', 'runtime-policy.json', 'README.md', 'METHODS.md', 'LICENSE', 'examples/java/pom.xml', 'examples/java/src/main/java/example/FirstBooking.java', 'examples/kotlin/pom.xml', 'examples/kotlin/src/main/kotlin/example/FirstBooking.kt', 'examples/kotlin-public/pom.xml', 'examples/kotlin-public/src/main/kotlin/example/PublicBooking.kt', 'examples/android/README.md', 'examples/android/consumer-rules.pro', 'CHANGELOG.md', 'SECURITY.md', 'SUPPORT.md', 'DEVELOPMENT.md', 'contract-projection.json')
JAVA_SOURCES = ('AvailKitException.java', 'ClientCore.java', 'ClientOptions.java', 'Environment.java', 'Models.java', 'PrivateFiles.java', 'Protocol.java', 'PublicBookingClient.java', 'RequestIntent.java', 'Secrets.java', 'Webhooks.java')

def check_sources(root):
    for module in ("public", "server"):
        secure_file(root, module + "/pom.xml")
        names = JAVA_SOURCES if module == "public" else ("WorkspaceClient.java",)
        allowed = {"java/com/availkit/" + name for name in names}
        allowed.add("resources/META-INF/LICENSE")
        if module == "public":
            allowed.add("resources/com/availkit/contract.json")
        source_root = root / module / "src/main"
        for path in source_root.rglob("*"):
            if path.is_symlink():
                raise ValueError("Symlink is not an SDK source input")
            if path.is_file() and path.relative_to(source_root).as_posix() not in allowed:
                raise ValueError("Unexpected SDK source input")
        for relative in allowed:
            secure_file(root, module + "/src/main/" + relative)

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--output", required=True)
    parser.add_argument("--skip-tests", action="store_true")
    args = parser.parse_args()
    root = pathlib.Path(__file__).resolve().parent
    out = pathlib.Path(args.output).resolve()
    out.mkdir(parents=True, exist_ok=True)
    for relative in EXAMPLE_FILES:
        secure_file(root, relative)
    check_sources(root)
    command = ["mvn", "-B", "-T1", "-Dstyle.color=never", "clean", "package"]
    if args.skip_tests:
        command.insert(3, "-DskipTests")
    subprocess.run(command, cwd=root, check=True)
    artifacts = []
    for module, artifact in (("public", "availkit-public"), ("server", "availkit")):
        destination = out / "maven/com/availkit" / artifact / VERSION
        destination.mkdir(parents=True, exist_ok=True)
        for suffix in (".jar", "-sources.jar", "-javadoc.jar", ".pom"):
            relative = module + "/pom.xml" if suffix == ".pom" else module + "/target/" + artifact + "-" + VERSION + suffix
            source = secure_file(root, relative)
            target = destination / (artifact + "-" + VERSION + suffix)
            shutil.copyfile(source, target)
            artifacts.append(target)
            for algorithm in ("sha256", "sha1", "sha512"):
                target.with_name(target.name + "." + algorithm).write_text(digest(target, algorithm) + "\n")
    archive = out / ("availkit-jvm-examples-" + VERSION + ".zip")
    write_examples(root, archive)
    archive.with_name(archive.name + ".sha256").write_text(digest(archive) + "  " + archive.name + "\n")
    artifacts.append(archive)
    print(json.dumps({"artifacts": [str(path.relative_to(out)) for path in artifacts]}))

if __name__ == "__main__":
    main()
