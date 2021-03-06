#include "presents/stable.h"
#include "BoxedBoolean.h"
#include "presents/ObjectInputStream.h"
#include "presents/ObjectOutputStream.h"
// Generated by running python boxer.py in this directory

using namespace presents::box;

DEFINE_STREAMABLE("java.lang.Boolean", BoxedBoolean);

void BoxedBoolean::readObject (ObjectInputStream& in)
{
    value = in.readBoolean();
}

void BoxedBoolean::writeObject (ObjectOutputStream& out) const
{
    out.writeBoolean(value);
}
