#pragma once
// Generated by running python boxer.py in this directory

#include "presents/Streamable.h"
#include "presents/streamers/StreamableStreamer.h"

namespace presents { namespace box {
    class BoxedShort : public Streamable{
    public:
        DECLARE_STREAMABLE();

        int16 value;

        static Shared<BoxedShort> createShared (int16 value)
        {
            Shared<BoxedShort> shared(new BoxedShort);
            shared->value = value;
            return shared;
        }

        virtual void readObject(ObjectInputStream& in);
        virtual void writeObject(ObjectOutputStream& out) const;
    };
}}