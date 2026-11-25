#!/usr/bin/env bash
# Downloads the TEXMEX SIFT datasets (http://corpus-texmex.irisa.fr/) into data/.
#   ./scripts/get-sift.sh          SIFT10K "siftsmall": 10k base x 128-d, 100 queries (~5 MB)
#   ./scripts/get-sift.sh --full   also SIFT1M "sift": 1M base, 10k queries (~160 MB download)
set -euo pipefail

BASE_URL="ftp://ftp.irisa.fr/local/texmex/corpus"
cd "$(dirname "$0")/.."
mkdir -p data
cd data

fetch() {
  local name="$1"
  if [[ -f "${name}/${name}_base.fvecs" ]]; then
    echo "data/${name} already present"
    return
  fi
  echo "downloading ${name}.tar.gz ..."
  wget -nc "${BASE_URL}/${name}.tar.gz"
  tar -xzf "${name}.tar.gz"
  rm "${name}.tar.gz"
  ls -lh "${name}"
}

fetch siftsmall
if [[ "${1:-}" == "--full" ]]; then
  fetch sift
fi
