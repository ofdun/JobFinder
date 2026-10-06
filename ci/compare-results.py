import sys
import xml.etree.ElementTree as ET
from pathlib import Path


def outcomes(directory):
    result = {}
    for path in Path(directory).glob("TEST-*.xml"):
        for case in ET.parse(path).getroot().findall("testcase"):
            key = (case.attrib["classname"], case.attrib["name"])
            result[key] = tuple(tag for tag in ("failure", "error", "skipped") if case.find(tag) is not None)
    if not result:
        raise SystemExit(f"No JUnit test cases in {directory}")
    return result


first, second = map(outcomes, sys.argv[1:3])
if first != second or any(second.values()):
    raise SystemExit("Repeated integration runs differ or contain unsuccessful cases")
print(f"Repeatability verified: {len(second)} identical successful cases")
