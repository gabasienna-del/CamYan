#!/bin/bash
git add .
git commit -m "${1:-update}"
git branch -M main
git push -u origin main
