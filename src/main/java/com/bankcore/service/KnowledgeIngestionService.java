package com.bankcore.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class KnowledgeIngestionService {

    private final VectorStore vectorStore;
    private final JdbcTemplate jdbcTemplate;

    private final TokenTextSplitter textSplitter =
            new TokenTextSplitter();

    @PostConstruct
    public void ingestKnowledge() {

        try {

            /*
             * ============================================================
             * CHECK WHETHER BANKCORE KNOWLEDGE IS ALREADY INGESTED
             * ============================================================
             *
             * We use the metadata field:
             *
             * type = bankcore-knowledge
             *
             * Every knowledge document inserted by this service contains
             * that metadata.
             *
             * If knowledge already exists, we skip ingestion to prevent
             * duplicate vectors every time the application restarts.
             */

            Integer existingKnowledgeCount = jdbcTemplate.queryForObject(
                    """
                    SELECT COUNT(*)
                    FROM vector_store
                    WHERE metadata->>'type' = 'bankcore-knowledge'
                    """,
                    Integer.class
            );

            if (existingKnowledgeCount != null
                    && existingKnowledgeCount > 0) {

                System.out.println(
                        "BankCore knowledge already exists in PGVector."
                );

                System.out.println(
                        "Found "
                                + existingKnowledgeCount
                                + " existing knowledge chunks."
                );

                System.out.println(
                        "Skipping knowledge ingestion to prevent duplicates."
                );

                return;
            }


            /*
             * ============================================================
             * LOAD KNOWLEDGE DOCUMENTS
             * ============================================================
             */

            PathMatchingResourcePatternResolver resolver =
                    new PathMatchingResourcePatternResolver();

            Resource[] resources =
                    resolver.getResources(
                            "classpath:/knowledge/*.md"
                    );

            if (resources.length == 0) {

                System.out.println(
                        "No knowledge documents found."
                );

                return;
            }


            /*
             * ============================================================
             * CREATE DOCUMENTS
             * ============================================================
             */

            List<Document> documents =
                    new ArrayList<>();

            for (Resource resource : resources) {

                String filename =
                        resource.getFilename();

                if (filename == null) {
                    continue;
                }

                String content =
                        resource.getContentAsString(
                                StandardCharsets.UTF_8
                        );

                Document document =
                        new Document(
                                content,
                                java.util.Map.of(
                                        "source",
                                        filename,

                                        "type",
                                        "bankcore-knowledge"
                                )
                        );

                documents.add(document);

                System.out.println(
                        "Loaded knowledge document: "
                                + filename
                );
            }


            /*
             * ============================================================
             * SPLIT DOCUMENTS INTO CHUNKS
             * ============================================================
             */

            List<Document> chunks =
                    textSplitter.split(documents);

            System.out.println(
                    "Generated "
                            + chunks.size()
                            + " knowledge chunks."
            );


            /*
             * ============================================================
             * STORE EMBEDDINGS IN PGVECTOR
             * ============================================================
             */

            vectorStore.write(chunks);

            System.out.println(
                    "Successfully stored knowledge embeddings in PGVector."
            );

        } catch (IOException e) {

            throw new IllegalStateException(
                    "Failed to load BankCore knowledge documents.",
                    e
            );
        }
    }
}