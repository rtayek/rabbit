#find log/files -name *.log -exec ls -l "{}" ";"
find log/files -type f -size 97c  -exec rm "{}" ";"
find log/files -type f -empty -exec rm "{}" ";"
#find log/files -name *.log -a -size 0 -exec ls -l "{}" ";"