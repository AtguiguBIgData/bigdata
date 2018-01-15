#!/bin/bash

# Author wuyufei

if [ -L ${BASH_SOURCE-$0} ]; then
  FWDIR=$(dirname $(readlink "${BASH_SOURCE-$0}"))
else
  FWDIR=$(dirname "${BASH_SOURCE-$0}")
fi

if [[ -z "${BIGDATA_HOME}" ]]; then
  # Make BIGDATA_HOME look cleaner in logs by getting rid of the
  # extra ../
  export BIGDATA_HOME="$(cd "${FWDIR}/.."; pwd)"
fi

if [[ -z "${BIGDATA_CONF_DIR}" ]]; then
  export BIGDATA_CONF_DIR="${BIGDATA_HOME}/conf"
fi

if [[ -z "${BIGDATA_LOG_DIR}" ]]; then
  export BIGDATA_LOG_DIR="${BIGDATA_HOME}/logs"
fi

if [[ -z "$BIGDATA_PID_DIR" ]]; then
  export BIGDATA_PID_DIR="${BIGDATA_HOME}/run"
fi

if [[ -z "${BIGDATA_WAR}" ]]; then
  if [[ -d "${BIGDATA_HOME}/web" ]]; then
    export BIGDATA_WAR="${BIGDATA_HOME}/web"
  else
    export BIGDATA_WAR=$(find -L "${BIGDATA_HOME}" -name "bigdata-web*.war")
  fi
fi


BIGDATA_CLASSPATH+=":${BIGDATA_CONF_DIR}"

function addEachJarInDir(){
  if [[ -d "${1}" ]]; then
    for jar in $(find -L "${1}" -maxdepth 1 -name '*jar'); do
      BIGDATA_CLASSPATH="$jar:$BIGDATA_CLASSPATH"
    done
  fi
}

function addEachJarInDirRecursive(){
  if [[ -d "${1}" ]]; then
    for jar in $(find -L "${1}" -type f -name '*jar'); do
      BIGDATA_CLASSPATH="$jar:$BIGDATA_CLASSPATH"
    done
  fi
}

function addEachJarInDirRecursiveForIntp(){
  if [[ -d "${1}" ]]; then
    for jar in $(find -L "${1}" -type f -name '*jar'); do
      BIGDATA_INTP_CLASSPATH="$jar:$BIGDATA_INTP_CLASSPATH"
    done
  fi
}

function addJarInDir(){
  if [[ -d "${1}" ]]; then
    BIGDATA_CLASSPATH="${1}/*:${BIGDATA_CLASSPATH}"
  fi
}

function addJarInDirForIntp() {
  if [[ -d "${1}" ]]; then
    BIGDATA_INTP_CLASSPATH="${1}/*:${BIGDATA_INTP_CLASSPATH}"
  fi
}


if [[ -z "${BIGDATA_ENCODING}" ]]; then
  export BIGDATA_ENCODING="UTF-8"
fi

if [[ -z "${BIGDATA_MEM}" ]]; then
  export BIGDATA_MEM="-Xms1024m -Xmx1024m -XX:MaxPermSize=512m"
fi


# Java Opts

JAVA_OPTS+=" ${BIGDATA_JAVA_OPTS} -Dfile.encoding=${BIGDATA_ENCODING} ${BIGDATA_MEM}"
JAVA_OPTS+=" -Dlog4j.configuration=file://${BIGDATA_CONF_DIR}/log4j.properties"
export JAVA_OPTS

# Java Process

if [[ -n "${JAVA_HOME}" ]]; then
  BIGDATA_RUNNER="${JAVA_HOME}/bin/java"
else
  BIGDATA_RUNNER=java
fi
export BIGDATA_RUNNER


if [[ -z "$BIGDATA_IDENT_STRING" ]]; then
  export BIGDATA_IDENT_STRING="${USER}"
fi
