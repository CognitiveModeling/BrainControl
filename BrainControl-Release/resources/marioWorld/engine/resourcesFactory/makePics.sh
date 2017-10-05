#creates multiply scaled copies of all images in this folder
#you can use this to downscale from original sprites to other resolutions
#here, it is used to upscale from the current folder

for res in "1280" "960" "640" "320"; do	
	for file in *.png; do
		file=`dirname -- "$file"`"/"`basename "$file" .png`

		if [[ "$file" == ../resolution"$res"/* ]]; then
			continue
		fi

		echo `basename "$file"`".png"

		convert "$file.png" -scale "`expr $res \* 100 / 1280`%" "../resources$res/$file.png"
	done

	for file in *.gif; do
		file=`dirname -- "$file"`"/"`basename "$file" .gif`

		if [[ "$file" == ../resolution"$res"/* ]]; then
			continue
		fi

		echo `basename "$file"`".gif"

		convert "$file.gif" -scale "`expr $res / 320`00%" "../resources$res/$file.gif"
	done
done
