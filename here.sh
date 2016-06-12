package=com.tayek.tablet.gui.android.cb7
dir=/data/data/$package/files
cat <<EOF | adb -s 0a9196e8 shell
run-as $package
ls -l /data/data/$package
ls -l $dir
exit
exit
EOF
echo foo
cat <<EOF | adb -s 0a9196e8 shell
run-as $package
echo #2
ls -l /data/data/$package
ls -l $dir
exit
exit
EOF