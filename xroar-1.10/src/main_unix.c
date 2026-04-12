/** \file
 *
 *  \brief main() function.
 *
 *  \copyright Copyright 2003-2024 Ciaran Anscomb
 *
 *  \licenseblock This file is part of XRoar, a Dragon/Tandy CoCo emulator.
 *
 *  XRoar is free software; you can redistribute it and/or modify it under the
 *  terms of the GNU General Public License as published by the Free Software
 *  Foundation, either version 3 of the License, or (at your option) any later
 *  version.
 *
 *  See COPYING.GPL for redistribution conditions.
 *
 *  \endlicenseblock
 */

#include "top-config.h"

#ifdef HAVE_WASM
#define SDL_MAIN_HANDLED
#endif

#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <unistd.h>

#ifdef HAVE_WASM
#include <emscripten/emscripten.h>

EM_JS(int, wasm_module_argc, (), {
	if (!Module.arguments || !Array.isArray(Module.arguments)) {
		return 0;
	}
	return Module.arguments.length;
});

EM_JS(int, wasm_module_argv_length, (int index), {
	if (!Module.arguments || !Array.isArray(Module.arguments) || index < 0 || index >= Module.arguments.length) {
		return 0;
	}
	return lengthBytesUTF8(String(Module.arguments[index])) + 1;
});

EM_JS(void, wasm_module_copy_argv, (int index, char *buffer, int buffer_size), {
	if (!buffer || buffer_size <= 0) {
		return;
	}
	if (!Module.arguments || !Array.isArray(Module.arguments) || index < 0 || index >= Module.arguments.length) {
		stringToUTF8("", buffer, buffer_size);
		return;
	}
	stringToUTF8(String(Module.arguments[index]), buffer, buffer_size);
});
#endif

#include "events.h"
#include "ui.h"
#include "xroar.h"
#include "logging.h"

#include "wasm/wasm.h"

/** \brief Entry point.
 *
 * Sets up the exit handler and calls xroar_init(), which will process all
 * configuration and return a UI interface.
 *
 * xroar_init_finish() is then called to finish initialisation and attach any
 * media.  If the interface provides its own run() method, it is called,
 * otherwise a default "main loop" repeatedly calls xroar_run().
 */

int main(int argc, char **argv) {
	atexit(xroar_shutdown);

	srand(getpid() ^ time(NULL));

#ifndef HAVE_WASM
	struct ui_interface *ui = xroar_init(argc, argv);
	if (!ui) {
		exit(EXIT_FAILURE);
	}

	// In normal builds, just finish up initialisation.
	xroar_init_finish();

	// If the UI interface provides its own run() delegate, just call that
	// (e.g. the GTK+ UI sets up emulated execution as an idle function and
	// passes control to GTK's own main loop).  Otherwise just repeatedly
	// call xroar_run().
	if (DELEGATE_DEFINED(ui->run)) {
		DELEGATE_CALL(ui->run);
	} else {
		for (;;) {
			xroar_run(EVENT_MS(10));
		}
	}
#else
	// The WebAssembly build has its own approach to completing
	// initialisation in order to allow initial required file transfers to
	// complete.
	wasm_init(argc, argv);
#endif
	return 0;
}

#ifdef HAVE_WASM
EMSCRIPTEN_KEEPALIVE int wasm_boot(void) {
	static int argc = 0;
	static char **argv = NULL;
	if (!argv) {
		int module_argc = wasm_module_argc();
		argc = module_argc + 1;
		argv = calloc((size_t)argc + 1, sizeof(*argv));
		if (!argv) {
			return EXIT_FAILURE;
		}
		argv[0] = strdup("xroar");
		if (!argv[0]) {
			return EXIT_FAILURE;
		}
		for (int i = 0; i < module_argc; ++i) {
			int length = wasm_module_argv_length(i);
			if (length <= 0) {
				continue;
			}
			argv[i + 1] = malloc((size_t)length);
			if (!argv[i + 1]) {
				return EXIT_FAILURE;
			}
			wasm_module_copy_argv(i, argv[i + 1], length);
		}
	}
	return main(argc, argv);
}
#endif
