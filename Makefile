
all: .gitignore

.gitignore: .gitignore.in
	rm -f $@
	sed -e 's/ *#.*$$//' <$< >$@
	chmod -w $@

ChangeLog:
	gitcl | gitcl2 -t uni -r -m 20 -n 2 >ChangeLog

services.list:
	find -name services -exec find {} -type f \; | sed -e 's,.*/,,g' | sort -u >$@

features.list:
	find -name features -exec find {} -type f \; | sed -e 's,.*/,,g' | sort -u >$@

