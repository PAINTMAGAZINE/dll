node {
   stage 'git'
   checkout([$class: 'GitSCM', branches: [[name: '*/master']], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'SubmoduleOption', disableSubmodules: false, recursiveSubmodules: true, reference: '', trackingSubmodules: false]], submoduleCfg: [], userRemoteConfigs: [[url: 'https://github.com/wichtounet/dll.git']]])

   stage 'pre-analysis'
   sh 'cppcheck --xml-version=2 --enable=all --std=c++11 include/dll/*.hpp test/src/*.cpp test_compile/*.cpp 2> cppcheck_report.xml'
   sh 'sloccount --duplicates --wide --details include/dll/*.hpp test/src/*.cpp test_compile/*.cpp  > sloccount.sc'
   sh 'cccc include/dll/*.hpp test/*.cpp test_compile/*.cpp || true'

   env.CXX="g++-4.9.3"
   env.LD="g++-4.9.3"
   env.ETL_MKL='true'
   env.DLL_COVERAGE='true'
   env.LD_LIBRARY_PATH="${env.LD_LIBRARY_PATH}:/opt/intel/mkl/lib/intel64"
   env.LD_LIBRARY_PATH="${env.LD_LIBRARY_PATH}:/opt/intel/lib/intel64"

   stage 'build'
   sh 'make clean'
   sh 'make -j6 release'

   stage 'test'
   sh './release/bin/dll_test -r junit -d yes -o catch_report.xml "[unit]" || true'
   sh 'gcovr -x -b -r . --object-directory=release/test > coverage_report.xml'

   stage 'sonar'
   sh '/opt/sonar-runner/bin/sonar-runner'
}
