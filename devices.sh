devices=`adb devices -l | tail -n +2 | tr -s ' ' | cut -d ' ' -f 1`
destination=log/files
package=com.tayek.tablet.gui.android.cb7
for i in $devices
	do
	echo $i
	adb devices -l | grep $i >/dev/null
		if [ $? -eq 0 ]
			then
			echo "$i is connected" >&2
			adb -s $i shell run-as $package ls -l files
			for j in 0 1 2 3 4
				do
				for i in 0 1 2 3 4 5 6 7 8 9
					do
						adb -s $device exec-out run-as $package cat files/tablet.$j.$i.log 2>$destination/$device.$j.$i.errors.txt > $destination/$device.$j.$i.log
					done
				done	
			else
			echo "$i is not connected" >&2
		fi
	done