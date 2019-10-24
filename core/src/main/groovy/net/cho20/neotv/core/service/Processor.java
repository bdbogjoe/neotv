package net.cho20.neotv.core.service;

import net.cho20.neotv.core.bean.Group;

public interface Processor {

    Iterable<Group> process() ;
}
