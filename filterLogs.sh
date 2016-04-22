for i in log/*
	do
	echo $i
	awk '$1 ~ /.*<message>report/, $1 ~ "</record>"' $i > $i.summary
	done 
mkdir log/summaries/
mv log/*.summary log/summaries/
find log/summaries/ -empty -type f -delete
