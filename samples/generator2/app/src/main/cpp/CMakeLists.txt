cmake_minimum_required(VERSION 3.22.1)

project("plasma")

add_subdirectory(audio)
INCLUDE_DIRECTORIES(audio)

option(BUILD_TESTS "Build tests" OFF)
option(ENABLE_FLOAT "single-precision" ON)
option(DISABLE_FORTRAN "Disable Fortran wrapper routines" ON)

add_subdirectory(fftw-3.3.9)
INCLUDE_DIRECTORIES(fftw-3.3.9/api)

#add_subdirectory(route)
#INCLUDE_DIRECTORIES(route)


add_library(${CMAKE_PROJECT_NAME} SHARED
        # List C/C++ source files with relative paths to this CMakeLists.txt.
        canvas.cpp
        generator2.cpp
        scope.cpp
        renderchannel.cpp

        plasma.cpp
        colormaps.cpp
        scale.cpp
        waterfall.cpp
        auformat.cpp

        route/global.cpp
        route/jniFFT.cpp


)

# Specifies libraries CMake should link to your target library. You
# can link libraries from various origins, such as libraries defined in this
# build script, prebuilt third-party libraries, or Android system libraries.
target_link_libraries(${CMAKE_PROJECT_NAME}
        # List libraries link to the target library
        android
        jnigraphics
        log
        audio
        fftw3f
)


set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS}")
# Укажите флаги компиляции для оптимизации на скорость -Ofast
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Ofast -Wall -static-openmp -std=c++17 -fPIC")