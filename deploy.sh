# choose deploy target dirs
n=LazyGui
t=out/deploy/LazyGui

echo "Cleaning $t..."
rm -rf $t/

echo "Deploying to $t..."
mkdir -p $t/data/ && cp -r data/ $t/
mkdir -p $t/src/ && cp -r src $t/
mkdir -p $t/reference/ && cp -r docs/* $t/reference
mkdir -p $t/examples/ && cp -r src/main/java/com/krab/lazy/examples $t/
mkdir -p $t/library/ && cp out/artifacts/LazyGui.jar $t/library/LazyGui.jar
cp library.properties $t/library.properties
cp README.md $t/README.md
cp LICENSE.md $t/src/LICENSE.md

echo "Zipping..."
7z a $n.zip $t

echo "Deployed LazyGui successfully."