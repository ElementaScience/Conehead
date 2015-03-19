#!/bin/sh
echo ""
echo "Copying ingest client artifacts to barman..."
cd ../out/artifacts/ch_jar/
cp * ../../../../barman/public/ingest/
echo "Done!"
