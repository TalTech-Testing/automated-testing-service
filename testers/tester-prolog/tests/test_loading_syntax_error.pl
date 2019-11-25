:- use_module('eplunit').

load_bad_code :-
    consult(predicate_with_syntax_error).

:- begin_tests(syntax, [ setup(load_bad_code) ]).

test(syntax_pass) :- correct.
test(syntax_unknown_predicate) :- invalid.

:- end_tests(syntax).
