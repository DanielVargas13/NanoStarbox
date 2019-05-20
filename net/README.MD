#### NanoStarbox: Net

*[Extended Library Class Path](./)*

This directory houses functionality which may be required by an
implementation that is not featured in the core library.

These libraries can be used via their official repository access URLs,
however any URL classloading should cache the downloaded data to an
implementation defined local path for both host and client, operational
efficiency.

The [NSR7A Specification](https://github.com/hypersoft/NanoStarbox/releases/tag/NSR7A) introduced [nano-jna-4.5.2-extended.jar](https://github.com/hypersoft/NanoStarbox/blob/NSR7/net/nano-jna-4.5.2-extended.jar) which houses
all jna libraries that are not included with the core, but are defined by
the core.

*NSR7 jna support was not officially enabled until NSR8*.

The [NSR8 Specification](https://github.com/hypersoft/NanoStarbox/releases/tag/NSR8) introduced machine architecture archives,
which includes all specifications not included with the core. Each machine dependent
archive is listed of the form: 'nano-jna-OS_NAME-OS_ARCH-4.5.2.jar', except in the case
of "darwin" OS which is located at 'nano-jna-darwin-4.5.2.jar'.
