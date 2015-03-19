#!/bin/bash
cd ../out/artifacts/ch_jar/
ssh -i ~/Documents/aws/rhplaykey.pem ec2-user@54.235.254.242 rm -rf /var/local/templates/journals/exp_template/webapp/ingest/*
scp -i ~/Documents/aws/rhplaykey.pem * ec2-user@54.235.254.242:/var/local/templates/journals/exp_template/webapp/ingest
