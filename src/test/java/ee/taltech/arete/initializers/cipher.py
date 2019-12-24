from itertools import cycle


def rail_pattern(n):
    r = list(range(n))
    return cycle(r + r[-2: 0: - 1])


def encode(a, b):
    p = rail_pattern(b)
    # this relies on key being called in order, guaranteed?
    return ''.join(sorted(a, key=lambda i: next(p))).replace(" ", "_")


def decode(a, b):
    p = rail_pattern(b)
    indexes = sorted(range(len(a)), key=lambda i: next(p))
    result = [''] * len(a)
    for i, c in zip(indexes, a):
        result[i] = c
    return ''.join(result).replace("_", "_")


print(encode("Mind on vaja krüpteerida", 3))  # => M_v_prido_aaküteiannjred
print(encode("Mind on", 3))  # => M_idonn
print(encode("hello", 1))  # => hello
print(encode("hello", 8))  # => hello
print(encode("kaks pead", 1))  # => kaks_pead

print(decode("kaks_pead", 1))  # => kaks pead
print(decode("M_idonn", 3))  # => Mind on
print(decode("M_v_prido_aaküteiannjred", 3))  # => Mind on vaja krüpteerida
