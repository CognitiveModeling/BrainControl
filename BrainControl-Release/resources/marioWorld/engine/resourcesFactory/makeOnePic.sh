#creates multiply scaled copies of one parametric image
#you can use this to downscale from original sprites to other resolutions

for res in "1280" "960" "640" "320"; do	
	file=`dirname -- "$1"`"/"`basename "$1" .png`

		if [[ "$file" == ../resolution"$res"/* ]]; then
			continue
		fi

		echo `basename "$file"`".png"

		convert "$file.png" -scale "`expr $res \* 100 / 1280`%" "../resources$res/$file.png"
done
