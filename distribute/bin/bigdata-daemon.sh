#!/bin/bash

# Author wuyufei

USAGE="-e Usage: bigdata-daemon.sh\n\t
        [--config <conf-dir>] {start|stop|restart|status}\n\t"

if [[ "$1" == "--config" ]]; then
  shift
  conf_dir="$1"
  if [[ ! -d "${conf_dir}" ]]; then
    echo "ERROR : ${conf_dir} is not a directory"
    echo ${USAGE}
    exit 1
  else
    export BIGDATA_CONF_DIR="${conf_dir}"
  fi
  shift
fi

if [ -L ${BASH_SOURCE-$0} ]; then
  BIN=$(dirname $(readlink "${BASH_SOURCE-$0}"))
else
  BIN=$(dirname ${BASH_SOURCE-$0})
fi
BIN=$(cd "${BIN}">/dev/null; pwd)

. "${BIN}/common.sh"
. "${BIN}/functions.sh"

HOSTNAME=$(hostname)
BIGDATA_NAME="Bigdata"
BIGDATA_LOGFILE="${BIGDATA_LOG_DIR}/bigdata-${BIGDATA_IDENT_STRING}-${HOSTNAME}.log"
BIGDATA_OUTFILE="${BIGDATA_LOG_DIR}/bigdata-${BIGDATA_IDENT_STRING}-${HOSTNAME}.out"
BIGDATA_PID="${BIGDATA_PID_DIR}/bigdata-${BIGDATA_IDENT_STRING}-${HOSTNAME}.pid"
BIGDATA_MAIN=com.atguigu.business.ApplicationServer
JAVA_OPTS+=" -Dbigdata.log.file=${BIGDATA_LOGFILE}"

# Add jdbc connector jar
# ZEPPELIN_CLASSPATH+=":${ZEPPELIN_HOME}/jdbc/jars/jdbc-connector-jar"

addJarInDir "${BIGDATA_HOME}"
addJarInDir "${BIGDATA_HOME}/lib"

CLASSPATH+=":${BIGDATA_CLASSPATH}"

if [[ "${BIGDATA_NICENESS}" = "" ]]; then
    export BIGDATA_NICENESS=0
fi

function initialize_default_directories() {
  if [[ ! -d "${BIGDATA_LOG_DIR}" ]]; then
    echo "Log dir doesn't exist, create ${BIGDATA_LOG_DIR}"
    $(mkdir -p "${BIGDATA_LOG_DIR}")
  fi

  if [[ ! -d "${BIGDATA_PID_DIR}" ]]; then
    echo "Pid dir doesn't exist, create ${BIGDATA_PID_DIR}"
    $(mkdir -p "${BIGDATA_PID_DIR}")
  fi
}

function wait_for_bigdata_to_die() {
  local pid
  local count
  pid=$1
  timeout=$2
  count=0
  timeoutTime=$(date "+%s")
  let "timeoutTime+=$timeout"
  currentTime=$(date "+%s")
  forceKill=1

  while [[ $currentTime -lt $timeoutTime ]]; do
    $(kill ${pid} > /dev/null 2> /dev/null)
    if kill -0 ${pid} > /dev/null 2>&1; then
      sleep 3
    else
      forceKill=0
      break
    fi
    currentTime=$(date "+%s")
  done

  if [[ forceKill -ne 0 ]]; then
    $(kill -9 ${pid} > /dev/null 2> /dev/null)
  fi
}

function wait_bigdata_is_up_for_ci() {
  if [[ "${CI}" == "true" ]]; then
    local count=0;
    while [[ "${count}" -lt 30 ]]; do
      curl -v localhost:8080 2>&1 | grep '200 OK'
      if [[ $? -ne 0 ]]; then
        sleep 1
        continue
      else
        break
      fi
        let "count+=1"
    done
  fi
}

function print_log_for_ci() {
  if [[ "${CI}" == "true" ]]; then
    tail -1000 "${BIGDATA_LOGFILE}" | sed 's/^/  /'
  fi
}

function check_if_process_is_alive() {
  local pid
  pid=$(cat ${BIGDATA_PID})
  if ! kill -0 ${pid} >/dev/null 2>&1; then
    action_msg "${BIGDATA_NAME} process died" "${SET_ERROR}"
    print_log_for_ci
    return 1
  fi
}

function start() {
  local pid

  if [[ -f "${BIGDATA_PID}" ]]; then
    pid=$(cat ${BIGDATA_PID})
    if kill -0 ${pid} >/dev/null 2>&1; then
      echo "${BIGDATA_NAME} is already running"
      return 0;
    fi
  fi

  initialize_default_directories

  echo "BIGDATA_CLASSPATH: ${BIGDATA_CLASSPATH_OVERRIDES}:${CLASSPATH}" >> "${BIGDATA_OUTFILE}"

  nohup nice -n $BIGDATA_NICENESS $BIGDATA_RUNNER $JAVA_OPTS -cp $BIGDATA_CLASSPATH_OVERRIDES:$CLASSPATH $BIGDATA_MAIN >> "${BIGDATA_OUTFILE}" 2>&1 < /dev/null &
  pid=$!
  if [[ -z "${pid}" ]]; then
    action_msg "${BIGDATA_NAME} start" "${SET_ERROR}"
    return 1;
  else
    action_msg "${BIGDATA_NAME} start" "${SET_OK}"
    echo ${pid} > ${BIGDATA_PID}
  fi

  wait_bigdata_is_up_for_ci
  sleep 2
  check_if_process_is_alive
}

function stop() {
  local pid

  # zeppelin daemon kill
  if [[ ! -f "${BIGDATA_PID}" ]]; then
    echo "${BIGDATA_NAME} is not running"
  else
    pid=$(cat ${BIGDATA_PID})
    if [[ -z "${pid}" ]]; then
      echo "${BIGDATA_NAME} is not running"
    else
      wait_for_bigdata_to_die $pid 40
      $(rm -f ${BIGDATA_PID})
      action_msg "${BIGDATA_NAME} stop" "${SET_OK}"
    fi
  fi

  # list all pid that used in remote interpreter and kill them
  for f in ${BIGDATA_PID_DIR}/*.pid; do
    if [[ ! -f ${f} ]]; then
      continue;
    fi

    pid=$(cat ${f})
    wait_for_bigdata_to_die $pid 20
    $(rm -f ${f})
  done

}

function find_bigdata_process() {
  local pid

  if [[ -f "${BIGDATA_PID}" ]]; then
    pid=$(cat ${BIGDATA_PID})
    if ! kill -0 ${pid} > /dev/null 2>&1; then
      action_msg "${BIGDATA_NAME} running but process is dead" "${SET_ERROR}"
      return 1
    else
      action_msg "${BIGDATA_NAME} is running" "${SET_OK}"
    fi
  else
    action_msg "${BIGDATA_NAME} is not running" "${SET_ERROR}"
    return 1
  fi
}

case "${1}" in
  start)
    start
    ;;
  stop)
    stop
    ;;
  restart)
    echo "${BIGDATA_NAME} is restarting" >> "${BIGDATA_OUTFILE}"
    stop
    start
    ;;
  status)
    find_bigdata_process
    ;;
  *)
    echo ${USAGE}
esac
