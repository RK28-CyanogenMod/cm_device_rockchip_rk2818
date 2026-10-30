import struct,sys
src=sys.argv[1]
out=sys.argv[2]
data=bytearray(open(src,'rb').read())
for old,new in [
(b'14ColorConvert164NewLEv',b'14RK28Convert5654NewLEv'),
(b'14MemoryHeapBaseC1EjjPKc',b'14RK28MemoryHeapC1EjjPKc'),
(b'15MediaPlayerBase',b'15RKMediaPlayBase'),
(b'20MediaPlayerInterface',b'20RKMediaPlayInterface'),
(b'22MediaPlayerHWInterface',b'22RKMediaPlayHWInterface')]:
 assert len(old)==len(new)
 data=data.replace(old,new)
# ICS bionic resolves a loaded object's direct dependencies, so the
# compatibility symbols must be an explicit DT_NEEDED dependency.
old=b'libsurfaceflinger_client.so'
new=b'librk28pvlegacy.so'
assert data.count(old)==1
data=data.replace(old,new+b'\0'*(len(old)-len(new)))
raw=bytes(data)
shoff=struct.unpack_from('<I',raw,32)[0]
shentsize,shnum=struct.unpack_from('<HH',raw,46)
sections=[struct.unpack_from('<10I',raw,shoff+i*shentsize) for i in range(shnum)]
sym=next(s for s in sections if s[1]==11)
strings=sections[sym[6]]
hashtab=next(s for s in sections if s[1]==5)
off=hashtab[4];nbucket,nchain=struct.unpack_from('<II',raw,off)
buckets=[0]*nbucket;chains=[0]*nchain
def elfhash(s):
 h=0
 for c in s:
  h=(h<<4)+c;g=h&0xf0000000
  if g:h^=g>>24
  h&=~g
 return h
for i in range(1,nchain):
 n=struct.unpack_from('<I',raw,sym[4]+i*sym[9])[0]+strings[4]
 name=data[n:data.find(b'\x00',n)]
 b=elfhash(name)%nbucket
 chains[i]=buckets[b];buckets[b]=i
data[off+8:off+8+4*(nbucket+nchain)]=struct.pack('<'+'I'*(nbucket+nchain),*(buckets+chains))
open(out,'wb').write(data)
print('PATCHED '+out)
