package=com.tayek.tablet.gui.android.cb7
cat <<EOF | adb -s 0a9196e8 shell
run-as $package
ls -l /data/data/$package
exit
exit
EOF