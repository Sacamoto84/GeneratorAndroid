#cmake_minimum_required(VERSION 3.4.1)

cmake_minimum_required(VERSION 3.18.1)

### INCLUDE OBOE LIBRARY ###

# Set the path to the Oboe library directory
#set (OBOE_DIR ../../../../../)
set (OBOE_DIR ../../../../../../)

# Add the Oboe library as a subproject. Since Oboe is an out-of-tree source library we must also
# specify a binary directory
add_subdirectory(${OBOE_DIR} ./oboe-bin)

# Include the Oboe headers and shared sample code
include_directories(${OBOE_DIR}/include ${OBOE_DIR}/samples/shared)

# Debug utilities
set (DEBUG_UTILS_PATH "${OBOE_DIR}/samples/debug-utils")
set (DEBUG_UTILS_SOURCES ${DEBUG_UTILS_PATH}/trace.cpp)
include_directories(${DEBUG_UTILS_PATH})


### END OBOE INCLUDE SECTION ###


# App specific sources
set (APP_SOURCES
    jni_bridge.cpp
    jni_bridge2.cpp
    jni_bridgeImpulse.cpp
    channel.cpp
    HelloOboeEngine.cpp
    generator.cpp
)

# Build the libhello-oboe library
add_library(app SHARED
            ${DEBUG_UTILS_SOURCES}
            ${APP_SOURCES}
            )

# Specify the libraries needed for hello-oboe
target_link_libraries(app android log oboe)

# Enable optimization flags: if having problems with source level debugging,
# disable -Ofast ( and debug ), re-enable after done debugging.

target_compile_options(app PRIVATE -Wall -Werror "$<$<CONFIG:RELEASE>:-Ofast>")
