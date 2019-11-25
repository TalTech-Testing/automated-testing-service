:- use_module('eplunit').

:- begin_tests(assertion).

test(pass_assertions) :-
    assertion(true).

test(fail_assertion) :-
    assertion(fail).

test(fail_assertion_2) :-
    assertion(1 = 2).

:- end_tests(assertion).
