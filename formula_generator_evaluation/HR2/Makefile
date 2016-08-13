CFLAGS = -Wall -O3

SRCS = HR2.cpp 
OBJS = $(SRCS:.cpp=.o)

HR2: $(OBJS)
	$(CXX) -o HR2 $(OBJS) $(LDFLAGS)

.cpp.o:
	$(CXX) $(CFLAGS) -c $< -o $@

all: HR2
	
clean:
	rm -f HR2
	rm -f HR2.exe
	rm -f *.o

