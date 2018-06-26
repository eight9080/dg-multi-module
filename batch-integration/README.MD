

Job called from 2 sources
1. rest controller -> sends message on the channel
http://localhost:8080/files?file=/Users/in.csv

2. scans a directory data/in for files

Both call same job which inserts data in a DB