print '>>> START'
if True:
    letterA = "a"
    letterB = "b"
    letterC = "c"
    letters = [ letterA, letterB, letterC ]
    
    for letter in letters:
        print 'letter: ' + letter + ' word:',
        
        if letter == 'a':
            print 'abacus'
        elif letter == 'b':
            print 'binoculars'
        elif letter == 'c':
            print 'circus'
else:
    print 'Hmmm...'
print '>>> END'