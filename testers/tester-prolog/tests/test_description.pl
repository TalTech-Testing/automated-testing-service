:- use_module('eplunit').

:- begin_tests(description).

test(pass, description("This test will pass")) :-
    true.

test(fail, description("This test will fail")) :-
    fail.

test(no_description) :-
    true.

test(multi_line_description, description("multi
line
description")) :-
    true.

:- end_tests(description).
