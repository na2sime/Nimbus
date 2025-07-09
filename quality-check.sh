#!/bin/sh

# Fix line endings on gradlew if necessary
echo "Checking gradlew line endings..."
if file gradlew | grep -q CRLF; then
  echo "Converting gradlew from CRLF to LF..."
  tr -d '\r' < gradlew > gradlew.tmp && mv gradlew.tmp gradlew
  chmod +x gradlew
fi

echo "Installing pre-commit hooks..."
python -m pre_commit install

echo "Running pre-commit checks..."
python -m pre_commit run --all-files || { echo "Pre-commit checks failed!"; exit 1; }

echo "All quality checks passed!"
