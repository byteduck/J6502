;Just a cool pattern program
loop:
ADC #1
INX
STA $0200,X
CMP #$ff
INX
INX
ADC #2
JMP loop