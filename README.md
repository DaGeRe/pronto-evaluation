# PRONTO-evaluation

This project evaluates the efficiency of the regression test selection of PRONTO (the regression test selection of Peass) against the regression test selections of Infinitest and EKSTAZI.

## Building

To build the project, execute the following steps:

1. Make infinitest available by running `git clone https://github.com/infinitest/infinitest.git && cd infinitest && mvn clean install -DskipTests`. This was evaluated using commit 35bb455, so if you are searching for a usable version, run `git checkout 35bb455` before. This only runs with Java 8.
2. Build PRONTO-evaluation: Go to the folder and run `mvn clean install`
