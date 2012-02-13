#!/bin/sh


dbname=
username=
hostname=
psql $dbname $username -h $hostname<< EOF
update lanecells set occupied=false;
EOF
