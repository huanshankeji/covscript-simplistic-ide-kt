# Creates a CovScript SDK directory containing symlinks to files and directories installed on Ubuntu
mkdir covscript
cd covscript


mkdir bin
cd bin
ln -s /usr/bin/cs cs
ln -s /usr/bin/cs_dbg cs_dbg
ln -s /usr/lib/csbuild csbuild
ln -s /usr/lib/cspkg cspkg
cd ..

ln -s /usr/share/covscript/imports imports

mkdir include
cd include
ln -s /usr/include/covscript covscript
cd ..

mkdir lib
cd lib
ln -s /usr/lib/libcovscript.a libcovscript.a
ln -s /usr/lib/libcovscript_debug.a libcovscript_debug.a
cd ..


cd ..