:- use_module('eplunit').

:- begin_tests(fixme).

test(fixme_pass, fixme(not_broken)) :-
    true.

test(fixme_fail, fixme(broken)) :-
    fail.

test(error, fixme(error)) :-
    _ is 3 / 0.

test(pass) :- true.
test(fail) :- fail.

:- end_tests(fixme).
