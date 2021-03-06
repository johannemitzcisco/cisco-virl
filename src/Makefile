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
#DIRS = ncsc-out ../load-dir ../shared-jar ../private-jar \
DIRS = ncsc-out ../load-dir ../shared-jar \
       java/src/$(JDIR)/$(NS)
NFXS = ncsc-out/modules/fxs
LOADDATASRC = $(wildcard load-data/*.xml)
LOADDATA = $(LOADDATASRC:load-data/%.xml=../load-dir/%.xml)

FXS = $(SRC:yang/%.yang=../load-dir/%.fxs)
PYTHONSRC = $(wildcard ../python/virl/*.py)
YANGPATH += ncsc-out/modules/yang
# 	../../<pkt-name>/src/yang
NCSCPATH   = $(YANGPATH:%=--yangpath %)



fxs: $(PYTHONSRC) $(DIRS) $(NFXS)/cisco-virl.fxs $(NFXS)/cisco-virl-stats.fxs $(FXS) $(LOADDATA)
# fxs: $(PYTHONSRC) $(DIRS) $(NFXS)/cisco-virl.fxs $(FXS)
.PHONY: fxs

$(PYTHONSRC): .FORCE
	python -m py_compile $@
.PHONY: .FORCE

$(DIRS):
	mkdir -p $@

$(NFXS)/cisco-virl-stats.fxs: yang/cisco-virl-stats.yang
	$(NCSC) --ncs-compile-module $< \
	        --ncs-device-dir ncsc-out \
	        --ncs-device-type generic-ned && cp $@ ../load-dir
	$(NCSC) $(JFLAGS)/ciscovirlStats.java $@

$(NFXS)/cisco-virl.fxs: yang/cisco-virl.yang
	$(NCSC) --ncs-compile-module $< \
	        --ncs-device-dir ncsc-out \
	        --ncs-skip-statistics \
	        --ncs-device-type generic-ned && cp $@ ../load-dir
	$(NCSC) $(JFLAGS)/ciscovirl.java $@

../load-dir/%.fxs: yang/%.yang
	$(NCSC)  `ls $*-ann.yang  > /dev/null 2>&1 && echo "-a $*-ann.yang"` \
             $(NCSCPATH) -c -o $@ $<

../load-dir/%.xml: load-data/%.xml
	cp $< $@

.PHONY: alljava

javac: fxs 
#	cp ../../java-lib/*.jar ../private-jar
	cd java && ant -q all
.PHONY: javac

clean:
	rm -rf $(DIRS)
	cd java && ant -q clean || true
.PHONY: clean
