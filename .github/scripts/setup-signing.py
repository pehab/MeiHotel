"""Restore a private signing identity; never log credentials or publish an ephemeral key."""
import base64
import json
import os
from pathlib import Path

raw = os.environ.get("MEIHOTEL_SIGNING_JSON", "")
if not raw:
    if os.environ.get("GITHUB_EVENT_NAME") != "pull_request":
        raise SystemExit("MEIHOTEL_SIGNING_JSON fehlt. Einmalig als Repository Actions Secret hinterlegen; keine APK mit wechselnder Signatur veröffentlichen.")
    print("Pull-request validation uses an ephemeral debug key; no APK release is published.")
else:
    data = json.loads(raw)
    password = data["password"]
    alias = data["alias"]
    if not password or any(ch in password + alias for ch in "\r\n"):
        raise SystemExit("Invalid signing secret")
    print("::add-mask::" + password)
    path = Path(os.environ["RUNNER_TEMP"]) / "meihotel-preview.p12"
    path.write_bytes(base64.b64decode(data["keystore"], validate=True))
    path.chmod(0o600)
    with open(os.environ["GITHUB_ENV"], "a", encoding="utf-8") as env:
        env.write("MEIHOTEL_KEYSTORE_FILE=" + str(path) + "\n")
        env.write("MEIHOTEL_KEYSTORE_PASSWORD=" + password + "\n")
        env.write("MEIHOTEL_KEY_ALIAS=" + alias + "\n")
