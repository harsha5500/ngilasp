#!/bin/sh


dbname="dmtest2"
username="postgres"
hostname="172.16.0.195"
psql $dbname $username -h $hostname<< EOF
update lanecells set occupied=false;
EOF
