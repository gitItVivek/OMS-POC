@echo off
REM OMS-POC JMeter benchmark runner (Windows)
REM Requires Apache JMeter 5.6+ on PATH as `jmeter`

setlocal
set ROOT=%~dp0..
set JMETER_PLAN=%ROOT%\benchmark\jmeter\oms-place-order-benchmark.jmx
set RESULTS=%ROOT%\benchmark\jmeter\results

if not exist "%RESULTS%" mkdir "%RESULTS%"

set PRODUCT_ID=%1
if "%PRODUCT_ID%"=="" (
  echo Usage: run-jmeter.bat ^<product-uuid^> [saga^|camel^|both]
  echo Example: run-jmeter.bat 11111111-2222-3333-4444-555555555555 saga
  exit /b 1
)

set MODE=%2
if "%MODE%"=="" set MODE=both

set TS=%date:~-4%%date:~-10,2%%date:~-7,2%_%time:~0,2%%time:~3,2%%time:~6,2%
set TS=%TS: =0%

echo Running JMeter benchmark mode=%MODE% product=%PRODUCT_ID%

jmeter -n -t "%JMETER_PLAN%" -l "%RESULTS%\%MODE%-%TS%.jtl" -e -o "%RESULTS%\report-%MODE%-%TS%" ^
  -JPRODUCT_ID=%PRODUCT_ID% ^
  -JBENCH_MODE=%MODE% ^
  -JIDENTITY_HOST=127.0.0.1 -JIDENTITY_PORT=8086 ^
  -JINTEGRATION_HOST=127.0.0.1 -JINTEGRATION_PORT=8085 ^
  -JTHREADS=10 -JRAMP_UP=5 -JLOOPS=20

echo Done. Open HTML report in benchmark\jmeter\results\report-%MODE%-%TS%\index.html
