# DSI Log Analyzer

Simple CLI extracting all trace messages concerning a given event.

## Compilation

`mvn clean install`

## Usage

Options:
* --display-all
* --event-id
* -h, --help
* --memento-key

Example to extract all trace messages concerning a given event from all
`trace.log` files under the current directory:

```
dsi-event-analyzer.sh --display-all --event-id C85AECEACABCC0CC4E11E7B22DAC5DDB .
```

In order to capture correctly the trace messages,
at least the log level of `com.ibm.ia.runtime.EventProcessor` must be set to `FINE`.

# Issues and contributions
For issues relating specifically to the Dockerfiles and scripts, please use the [GitHub issue tracker](../../issues).
We welcome contributions following [our guidelines](CONTRIBUTING.md).

# License
The Dockerfiles and associated scripts found in this project are licensed under the [Apache License 2.0](LICENSE).

# Notice
© Copyright IBM Corporation 2017.
