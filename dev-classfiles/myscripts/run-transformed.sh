#bin/bash
cd "$(dirname "$0")"
#echo $PWD
java -cp \
../mylibs/kotlin-stdlib-1.3.72.jar:\
../../core-api/build/libs/core-api.jar:\
../../testlambdas-api/build/libs/testlambdas-api.jar:\
../../testsql-api/build/libs/testsql-api.jar:\
../build/transformed-classes-2/kotlin/main com.almazsh.lambda.dev.classfiles.MainKt
