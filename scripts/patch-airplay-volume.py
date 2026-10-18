#!/usr/bin/env python3
"""Preserve the TV's existing hardware volume during AirPlay setup.

Applied to a disposable copy of doubletake while building. The upstream project
sends 'volume: 0.000000' (0 dB, maximum) after RECORD. On some TV receivers this
forces the TV volume to 100%. AirLink deliberately skips this unsolicited command.
"""
from pathlib import Path
import sys

if len(sys.argv) != 2:
    raise SystemExit("Usage: patch-airplay-volume.py <doubletake mirror.go>")
path = Path(sys.argv[1])
source = path.read_text()
start_marker = "\t// Set volume to 0 dB (full scale). Positive dB values are invalid here and"
end_marker = "\n\tif timingProtocol == timingProtocolPTP {"
if source.count(start_marker) != 1:
    raise SystemExit("AirPlay setup volume marker changed: refusing an unsafe patch")
start = source.index(start_marker)
end = source.index(end_marker, start)
block = source[start:end]
if 'SET_PARAMETER' not in block or 'volumeBody' not in block:
    raise SystemExit("AirPlay setup volume block unexpected: refusing patch")
source = (source[:start] +
    "\t// Eaton AirLink: preserve the receiver's own volume on connect.\n" +
    "\t// Do not send SET_PARAMETER volume=0 (full scale).\n" +
    source[end:])
path.write_text(source)
print("Eaton AirLink: removed automatic AirPlay receiver-volume override")
