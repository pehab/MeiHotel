"""Publish the signed versioned APK first, then advertise it through the preview feed."""
import hashlib
import json
import os
from pathlib import Path
import subprocess

apk=Path('MeiHotel.apk')
if not apk.exists():
    Path('app-debug.apk').rename(apk)
metadata=json.loads(Path('output-metadata.json').read_text())['elements'][0]
code=int(metadata['versionCode'])
name=metadata['versionName']
if code<=0 or not all(c.isalnum() or c in '.+-_' for c in name):
    raise SystemExit('Invalid APK version metadata')
tag=f'v{name}-preview-{code}'
url=f'https://github.com/pehab/MeiHotel/releases/download/{tag}/MeiHotel.apk'
manifest={'schema':1,'versionCode':code,'versionName':name,'url':url,'sha256':hashlib.sha256(apk.read_bytes()).hexdigest(),'size':apk.stat().st_size}
Path('MeiHotel-update.json').write_text(json.dumps(manifest,indent=2)+'\n')

def publish(release,title):
    found=subprocess.run(['gh','release','view',release],stdout=subprocess.DEVNULL,stderr=subprocess.DEVNULL).returncode==0
    if not found:
        subprocess.run(['gh','release','create',release,'--target',os.environ['GITHUB_SHA'],'--prerelease','--title',title,'--notes','Kindgerechte Testversion. APK herunterladen und über die bestehende Installation installieren. Der Spielstand bleibt bei gleicher Signatur erhalten.'],check=True)
    subprocess.run(['gh','release','upload',release,'MeiHotel.apk','MeiHotel-update.json','--clobber'],check=True)

publish(tag,f'MeiHotel {name}')
publish('preview','MeiHotel – aktuelle Testversion')
print(f'Published MeiHotel {name} ({code}) with SHA-256 update metadata.')
