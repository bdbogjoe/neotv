package net.cho20.neotv.core.service;

import java.util.concurrent.ExecutorService;

import net.cho20.neotv.core.bean.Group;
import net.cho20.neotv.core.bean.StreamBean;

public interface Processor {

    Iterable<Group<StreamBean>> process(ExecutorService executorService) ;
}
