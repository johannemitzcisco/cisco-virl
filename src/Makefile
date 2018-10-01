all: javac
.PHONY: all

# Include standard NCS examples build definitions and rules
include $(NCS_DIR)/src/ncs/build/include.ncs.mk

JAVA_PACKAGE = com.example.ciscovirl
JDIR = $(shell echo $(JAVA_PACKAGE) | sed 's/\./\//g')

NS  = namespaces

JFLAGS = --java-disable-prefix \
         --exclude-enums \
         --fail-on-warnings \
         --java-package $(JAVA_PACKAGE).$(NS) \
         --emit-java java/src/$(JDIR)/namespaces

SRC = $(wildcard yang/*.yang)
DIRS = ncsc-out ../load-dir ../shared-jar ../private-jar \
       java/src/$(JDIR)/$(NS)
NFXS = ncsc-out/modules/fxs

fxs: $(DIRS) $(NFXS)/cisco-virl.fxs $(NFXS)/cisco-virl-stats.fxs
.PHONY: fxs

$(DIRS):
	mkdir -p $@

$(NFXS)/cisco-virl.fxs: yang/cisco-virl.yang
	$(NCSC) --ncs-compile-module $< \
	        --ncs-device-dir ncsc-out \
	        --ncs-skip-statistics \
	        --ncs-device-type generic-ned && cp $@ ../load-dir
	$(NCSC) $(JFLAGS)/ciscovirl.java $@

$(NFXS)/cisco-virl-stats.fxs: yang/cisco-virl-stats.yang
	$(NCSC) --ncs-compile-module $< \
	        --ncs-device-dir ncsc-out \
	        --ncs-skip-template \
	        --ncs-device-type generic-ned && cp $@ ../load-dir
	$(NCSC) $(JFLAGS)/ciscovirlStats.java $@

javac: fxs
	cp ../../java-lib/*.jar ../private-jar
	cd java && ant -q all
.PHONY: javac

clean:
	rm -rf $(DIRS)
	cd java && ant -q clean || true
.PHONY: clean
