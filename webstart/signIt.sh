#!/bin/bash
FILES=../out/artifacts/ch_jar/*.jar
for f in $FILES
do
  echo ""
  echo "about to sign $f"
  jarsigner -keystore ingestKeystore -storepass abcdefg1 $f dg
done

echo ""
echo "copying in IngestTool.jnlp"
cp IngestTool.jnlp ../out/artifacts/ch_jar/