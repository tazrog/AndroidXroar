/* config.h.  Generated from config.h.in by configure.  */
/* config.h.in.  Generated from configure.ac by autoheader.  */

/* Define if building universal (internal helper macro) */
/* #undef AC_APPLE_UNIVERSAL_BUILD */

/* Snapshot build */
/* #undef ENABLE_SNAPSHOT */

/* Use ALSA audio */
#define HAVE_ALSA_AUDIO 1

/* Correct-endian architecture */
/* #undef HAVE_BIG_ENDIAN */

/* Include CLI file requester */
/* #undef HAVE_CLI */

/* Have Mac OS X Cocoa framework */
/* #undef HAVE_COCOA */

/* Define to 1 if you have the <endian.h> header file. */
#define HAVE_ENDIAN_H 1

/* Use libevdev */
/* #undef HAVE_EVDEV */

/* Define to 1 if the system has the `const' function attribute */
#define HAVE_FUNC_ATTRIBUTE_CONST 1

/* Define to 1 if the system has the `format' function attribute */
#define HAVE_FUNC_ATTRIBUTE_FORMAT 1

/* Define to 1 if the system has the `malloc' function attribute */
#define HAVE_FUNC_ATTRIBUTE_MALLOC 1

/* Define to 1 if the system has the `nonnull' function attribute */
#define HAVE_FUNC_ATTRIBUTE_NONNULL 1

/* Define to 1 if the system has the `noreturn' function attribute */
#define HAVE_FUNC_ATTRIBUTE_NORETURN 1

/* Define to 1 if the system has the `pure' function attribute */
#define HAVE_FUNC_ATTRIBUTE_PURE 1

/* Define to 1 if the system has the `returns_nonnull' function attribute */
#define HAVE_FUNC_ATTRIBUTE_RETURNS_NONNULL 1

/* Define to 1 if you have the 'getaddrinfo' function. */
#define HAVE_GETADDRINFO 1

/* Defined if a valid OpenGL implementation is found. */
#define HAVE_GL 1

/* Define to 1 if you have the <GL/gl.h> header file. */
#define HAVE_GL_GL_H 1

/* Use GTK+ 2 */
/* #undef HAVE_GTK2 */

/* Use GTK+ 3 */
#define HAVE_GTK3 1

/* Use GtkGLExt */
/* #undef HAVE_GTKGL */

/* Define to 1 if you have the <inttypes.h> header file. */
#define HAVE_INTTYPES_H 1

/* Use JACK audio */
/* #undef HAVE_JACK_AUDIO */

/* Have linux/joystick.h */
#define HAVE_JOYDEV 1

/* Have Mac OS X Core Audio framework */
/* #undef HAVE_MACOSX_AUDIO */

/* Include null audio driver */
#define HAVE_NULL_AUDIO 1

/* Define to 1 if you have the <OpenGL/gl.h> header file. */
/* #undef HAVE_OPENGL_GL_H */

/* Use OSS audio */
#define HAVE_OSS_AUDIO 1

/* Have libpng */
#define HAVE_PNG 1

/* Define to 1 if you have the 'popen' function. */
#define HAVE_POPEN 1

/* POSIX threads */
#define HAVE_PTHREADS 1

/* Have PulseAudio */
#define HAVE_PULSE 1

/* Define to 1 if you have the <regex.h> header file. */
#define HAVE_REGEX_H 1

/* Have SDL2 */
#define HAVE_SDL2 1

/* Have SDL2_image */
/* #undef HAVE_SDL2_IMAGE */

/* Use libsndfile */
/* #undef HAVE_SNDFILE */

/* Define to 1 if you have the <stdint.h> header file. */
#define HAVE_STDINT_H 1

/* Define to 1 if you have the <stdio.h> header file. */
#define HAVE_STDIO_H 1

/* Define to 1 if you have the <stdlib.h> header file. */
#define HAVE_STDLIB_H 1

/* Define to 1 if you have the <strings.h> header file. */
#define HAVE_STRINGS_H 1

/* Define to 1 if you have the <string.h> header file. */
#define HAVE_STRING_H 1

/* Define to 1 if you have the 'strnlen' function. */
#define HAVE_STRNLEN 1

/* Define to 1 if you have the 'strsep' function. */
#define HAVE_STRSEP 1

/* Define to 1 if you have the <sys/stat.h> header file. */
#define HAVE_SYS_STAT_H 1

/* Define to 1 if you have the <sys/types.h> header file. */
#define HAVE_SYS_TYPES_H 1

/* TRE */
/* #undef HAVE_TRE */

/* Define to 1 if you have the <unistd.h> header file. */
#define HAVE_UNISTD_H 1

/* Define to 1 if the system has the `packed' variable attribute */
#define HAVE_VAR_ATTRIBUTE_PACKED 1

/* WebAssembly build */
/* #undef HAVE_WASM */

/* Define to 1 if you have the <windows.h> header file. */
/* #undef HAVE_WINDOWS_H */

/* Have xlib */
#define HAVE_X11 1

/* Have zlib */
#define HAVE_ZLIB 1

/* Define to 1 if the system has the `__builtin_expect' built-in function */
#define HAVE___BUILTIN_EXPECT 1

/* Define to 1 if the system has the `__builtin_parity' built-in function */
#define HAVE___BUILTIN_PARITY 1

/* Logging */
#define LOGGING 1

/* Name of package */
#define PACKAGE "xroar"

/* Define to the address where bug reports for this package should be sent. */
#define PACKAGE_BUGREPORT ""

/* Define to the full name of this package. */
#define PACKAGE_NAME "xroar"

/* Define to the full name and version of this package. */
#define PACKAGE_STRING "xroar 1.10"

/* Define to the one symbol short name of this package. */
#define PACKAGE_TARNAME "xroar"

/* Visible package name */
#define PACKAGE_TEXT "XRoar 1.10"

/* Define to the home page for this package. */
#define PACKAGE_URL ""

/* Define to the version of this package. */
#define PACKAGE_VERSION "1.10"

/* Package year */
#define PACKAGE_YEAR "2026"

/* Snapshot major */
#define RC_REV_MAJOR 0

/* Snapshot minor */
#define RC_REV_MINOR 0

/* Screenshots supported */
#define SCREENSHOT 1

/* The size of 'double', as computed by sizeof. */
#define SIZEOF_DOUBLE 8

/* The size of 'float', as computed by sizeof. */
#define SIZEOF_FLOAT 4

/* Define to 1 if all of the C89 standard headers exist (not just the ones
   required in a freestanding environment). This macro is provided for
   backward compatibility; new code need not use it. */
#define STDC_HEADERS 1

/* Support trace mode */
#define TRACE 1

/* Version number of package */
#define VERSION "1.10"

/* Becker port */
#define WANT_BECKER 1

/* Want Dragon/Tandy CoCo cartridges */
#define WANT_CART_ARCH_DRAGON 1

/* Want Tandy MC-10 cartridges */
#define WANT_CART_ARCH_MC10 1

/* Want experimental/incomplete code */
/* #undef WANT_EXPERIMENTAL */

/* GDB target */
#define WANT_GDB_TARGET 1

/* Want Tandy Coco 3 */
#define WANT_MACHINE_ARCH_COCO3 1

/* Want Dragon, Tandy Coco 1/2 */
#define WANT_MACHINE_ARCH_DRAGON 1

/* Want Tandy MC-10 */
#define WANT_MACHINE_ARCH_MC10 1

/* Want MC6801/MC6803 CPU support */
#define WANT_PART_MC6801 1

/* Want MC6809/HD6309 CPU support */
#define WANT_PART_MC6809 1

/* Want MC6821 PIA support */
#define WANT_PART_MC6821 1

/* Want MC6847 VDG support */
#define WANT_PART_MC6847 1

/* Want MC6883 SAM support */
#define WANT_PART_MC6883 1

/* Want TCC1014 GIME support */
#define WANT_PART_TCC1014 1

/* Traps */
#define WANT_TRAPS 1

/* Enable basic SDL UI */
#define WANT_UI_SDL 1

/* Windows */
/* #undef WINDOWS32 */

/* Define WORDS_BIGENDIAN to 1 if your processor stores words with the most
   significant byte first (like Motorola and SPARC, unlike Intel). */
#if defined AC_APPLE_UNIVERSAL_BUILD
# if defined __BIG_ENDIAN__
#  define WORDS_BIGENDIAN 1
# endif
#else
# ifndef WORDS_BIGENDIAN
/* #  undef WORDS_BIGENDIAN */
# endif
#endif

/* Define to 1 if the X Window System is missing or not being used. */
/* #undef X_DISPLAY_MISSING */

/* Number of bits in a file offset, on hosts where this is settable. */
/* #undef _FILE_OFFSET_BITS */

/* Define to 1 on platforms where this makes off_t a 64-bit type. */
/* #undef _LARGE_FILES */

/* Number of bits in time_t, on hosts where this is settable. */
/* #undef _TIME_BITS */

/* Define for Solaris 2.5.1 so the uint32_t typedef from <sys/synch.h>,
   <pthread.h>, or <semaphore.h> is not used. If the typedef were allowed, the
   #define below would cause a syntax error. */
/* #undef _UINT32_T */

/* Define for Solaris 2.5.1 so the uint8_t typedef from <sys/synch.h>,
   <pthread.h>, or <semaphore.h> is not used. If the typedef were allowed, the
   #define below would cause a syntax error. */
/* #undef _UINT8_T */

/* Define to 1 on platforms where this makes time_t a 64-bit type. */
/* #undef __MINGW_USE_VC2005_COMPAT */

/* Define to the type of a signed integer type of width exactly 16 bits if
   such a type exists and the standard includes do not define it. */
/* #undef int16_t */

/* Define to the type of a signed integer type of width exactly 32 bits if
   such a type exists and the standard includes do not define it. */
/* #undef int32_t */

/* Define to the type of a signed integer type of width exactly 64 bits if
   such a type exists and the standard includes do not define it. */
/* #undef int64_t */

/* Define to the type of a signed integer type of width exactly 8 bits if such
   a type exists and the standard includes do not define it. */
/* #undef int8_t */

/* Define to 'long int' if <sys/types.h> does not define. */
/* #undef off_t */

/* Define as 'unsigned int' if <stddef.h> doesn't define. */
/* #undef size_t */

/* Define as 'int' if <sys/types.h> doesn't define. */
/* #undef ssize_t */

/* Define to the type of an unsigned integer type of width exactly 16 bits if
   such a type exists and the standard includes do not define it. */
/* #undef uint16_t */

/* Define to the type of an unsigned integer type of width exactly 32 bits if
   such a type exists and the standard includes do not define it. */
/* #undef uint32_t */

/* Define to the type of an unsigned integer type of width exactly 8 bits if
   such a type exists and the standard includes do not define it. */
/* #undef uint8_t */
